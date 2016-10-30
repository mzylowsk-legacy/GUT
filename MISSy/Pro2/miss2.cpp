#include "gmp.h"
#include <cstdlib>

struct element {
    mpq_t value;
    element* next;
};
element *first, *last;

void clearList();
void print_haxx(mpz_t, mpz_t, mpz_t);
int readData();
void kolomogorow(int, mpq_t*, mpz_t);

int main(int argc, char* argv[])
{
    int n = readData();
    mpq_t N;
    mpq_init(N);
    mpq_set_d(N, n);
    int j = 0, e = 0, i = 0;
    mpz_t precision;
    mpz_init(precision);
    mpz_ui_pow_ui(precision, 10, atoi(argv[1]));
    
    mpq_t x;
    mpq_init(x);

    element* tmp;
    tmp = first;
    
    mpq_t* data = new mpq_t[n];
    while (tmp != NULL) {
        mpq_init(data[i]);
        mpq_set(data[i], tmp->value);
        tmp = tmp->next;
        i++;
    }
    clearList();
    
    mpq_t s1, s2, half, m1, m2, av, one, b, a;
    mpq_inits(s1, s2, a, half, m1, m2, av, one, b, NULL);
    mpq_set_d(half, 0.5);
    mpq_set_d(av, 0.25);

    for (int i = 0; i < n; i++) {
		for (int j = 0; j < n - i - 1; j++) {
			if (mpq_cmp(data[j], data[j + 1]) > 0) {
                mpq_swap(data[j], data[j + 1]);
            }
		}
	}

    int k = 10;
    i = 0;
    mpq_t* tabA = new mpq_t[k + 1];
    mpq_t range, in, sto, zero;
    mpq_inits(range, a, m1, in, sto, zero, NULL);
    mpq_set_d(sto, 100);
    mpq_set_d(zero, 0);
    mpq_t* TAB = new mpq_t[k + 1];
    
    while (i <= k) {
        mpq_init(TAB[i]);
        mpq_set_d(in, i);
        mpq_mul(m1, in, in);
        mpq_div(a, m1, sto);
        mpq_init(tabA[i]);
        mpq_set(tabA[i], a);
        if (i >= 1)
            mpq_sub(TAB[i], tabA[i], tabA[i - 1]);
        else
            mpq_set(TAB[i], zero);
        i++;
    }
    
    int l = 0;
    mpq_t* TAByi = new mpq_t[k + 1];
    i = 1;
    int count;
    mpq_init(TAByi[0]);
    
    while (i <= k) {
        mpq_init(TAByi[i]);
        count = 0;
        for (l = 0; l < n; l++) {
            if ((mpq_cmp(data[l], tabA[i]) < 0) && 
            ((mpq_cmp(data[l], tabA[i - 1]) > 0 || 
            (mpq_equal(data[l], tabA[i - 1]) > 0))))
                count++;
        }
        mpq_set_d(TAByi[i], count);
        i++;
    }
    
    i = 1;
    mpq_t sq, subYIn, divKWn, SUMV, MULn;
    mpq_inits(sq, subYIn, divKWn, SUMV, MULn, NULL);
    
    while (i <= k) {
        mpq_mul(MULn, TAB[i], N);
        mpq_sub(subYIn, TAByi[i], MULn);
        mpq_mul(sq, subYIn, subYIn);
        mpq_div(divKWn, sq, MULn);
        mpq_add(SUMV, SUMV, divKWn);
        i++;
    }
    mpz_t Vnum, Vden;
    mpz_inits(Vnum, Vden, NULL);
    mpq_get_num(Vnum, SUMV);
    mpq_get_den(Vden, SUMV);
    print_haxx(Vnum, Vden, precision);

	kolomogorow(n, data, precision);
    return 0;
}

void clearList()
{
    element* elem2;
    element* elem = first;
    while (elem != NULL) {
        elem2 = elem->next;
        delete (elem);
        elem = elem2;
    }
}

void print_haxx(mpz_t v1, mpz_t v2, mpz_t precision)
{
    mpz_t a, b, c, precision2;
    mpz_inits(a, b, c, precision2, NULL);
    mpz_set(precision2, precision);
    mpz_tdiv_qr(b, a, v1, v2);
    mpz_mul(a, a, precision2);
    mpz_tdiv_q(c, a, v2);
    mpz_abs(c, c);
    gmp_printf("%Zd", b);
    if (mpz_sgn(c) != 0) {
        gmp_printf(".");
        mpz_tdiv_q_ui(precision2, precision2, 10);
        while (mpz_cmp(c, precision2) < 0) {
            gmp_printf("0");
            mpz_tdiv_q_ui(precision2, precision2, 10);
        }
        if (mpz_sgn(c) != 0) {
            while (mpz_divisible_ui_p(c, 10)) {
                mpz_cdiv_q_ui(c, c, 10);
            }
            gmp_printf("%Zd", c);
        }
    }
    printf("\n");
}

int readData()
{		
	int n = 0;
	mpq_t newValue;
	mpq_init(newValue);
	while (mpq_inp_str(newValue, stdin, 10)) 
	{
		fflush(stdin);
		n++;
		if (first == NULL) {
			first = new element;
			first->next = NULL;
			mpq_init(first->value);
			mpq_set(first->value, newValue);
			last = first;
		}
        else {
            element* newElem = new element();
            newElem->next = NULL;
            mpq_init(newElem->value);
            mpq_set(newElem->value, newValue);
            last->next = newElem;
            last = newElem;
        }
    }
    return n;
}

void kolomogorow(int n, mpq_t* data, mpz_t precision) {
    mpq_t N, one, J, DIVjn, SUBjf, DIVxn, maxKplus, maxKminus, SUBone;
    mpq_inits(N, one, J, DIVjn, SUBjf, DIVxn, maxKplus, maxKminus, SUBone, NULL);
    mpq_set_d(N, n);
    mpq_set_d(one, 1);
    mpq_t* Kplus = new mpq_t[n];
    mpq_t* Kminus = new mpq_t[n];
    for (int j = 0; j < n; j++) {
        mpq_init(Kplus[j]);
        mpq_init(Kminus[j]);
        mpq_set_d(J, j + 1);
        mpq_div(DIVjn, J, N);
        mpq_div(DIVxn, data[j], one);
        mpq_sub(SUBjf, DIVjn, DIVxn);
        mpq_set(Kplus[j], SUBjf);
        mpq_sub(SUBone, J, one);
        mpq_div(DIVjn, SUBone, N);
        mpq_sub(SUBjf, DIVxn, DIVjn);
        mpq_set(Kminus[j], SUBjf);
    }

    mpq_set(maxKplus, Kplus[0]);
    mpq_set(maxKminus, Kminus[0]);
    for (int i = 1; i < n; i++) {
        if (mpq_cmp(maxKplus, Kplus[i]) < 0) {
            mpq_set(maxKplus, Kplus[i]);
        }
        if (mpq_cmp(maxKminus, Kminus[i]) < 0) {
            mpq_set(maxKminus, Kminus[i]);
        }
    }

    mpz_t KPLUSnum, KPLUSden, KMINUSnum, KMINUSden;
    mpz_inits(KPLUSnum, KPLUSden, KMINUSnum, KMINUSden, NULL);
    mpq_get_num(KPLUSnum, maxKplus);
    mpq_get_den(KPLUSden, maxKplus);
    print_haxx(KPLUSnum, KPLUSden, precision);
    mpq_get_num(KMINUSnum, maxKminus);
    mpq_get_den(KMINUSden, maxKminus);
    print_haxx(KMINUSnum, KMINUSden, precision);
}
